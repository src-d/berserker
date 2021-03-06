// Code generated by protoc-gen-gogo. DO NOT EDIT.
// source: github.com/src-d/berserker/extractor/generated.proto

/*
	Package extractor is a generated protocol buffer package.

	It is generated from these files:
		github.com/src-d/berserker/extractor/generated.proto

	It has these top-level messages:
		File
		RepositoryData
		Request
		Service_GetRepositoriesDataResponse
*/
package extractor

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"
import _ "github.com/gogo/protobuf/gogoproto"

import (
	context "golang.org/x/net/context"
	grpc "google.golang.org/grpc"
)

import io "io"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

func (m *File) Reset()                    { *m = File{} }
func (m *File) String() string            { return proto.CompactTextString(m) }
func (*File) ProtoMessage()               {}
func (*File) Descriptor() ([]byte, []int) { return fileDescriptorGenerated, []int{0} }

func (m *RepositoryData) Reset()                    { *m = RepositoryData{} }
func (m *RepositoryData) String() string            { return proto.CompactTextString(m) }
func (*RepositoryData) ProtoMessage()               {}
func (*RepositoryData) Descriptor() ([]byte, []int) { return fileDescriptorGenerated, []int{1} }

func (m *Request) Reset()                    { *m = Request{} }
func (m *Request) String() string            { return proto.CompactTextString(m) }
func (*Request) ProtoMessage()               {}
func (*Request) Descriptor() ([]byte, []int) { return fileDescriptorGenerated, []int{2} }

type Service_GetRepositoriesDataResponse struct {
	Result1 []*RepositoryData `protobuf:"bytes,1,rep,name=result1" json:"result1,omitempty"`
}

func (m *Service_GetRepositoriesDataResponse) Reset()         { *m = Service_GetRepositoriesDataResponse{} }
func (m *Service_GetRepositoriesDataResponse) String() string { return proto.CompactTextString(m) }
func (*Service_GetRepositoriesDataResponse) ProtoMessage()    {}
func (*Service_GetRepositoriesDataResponse) Descriptor() ([]byte, []int) {
	return fileDescriptorGenerated, []int{3}
}

func (m *Service_GetRepositoriesDataResponse) GetResult1() []*RepositoryData {
	if m != nil {
		return m.Result1
	}
	return nil
}

func init() {
	proto.RegisterType((*File)(nil), "github.com.srcd.berserker.extractor.File")
	proto.RegisterType((*RepositoryData)(nil), "github.com.srcd.berserker.extractor.RepositoryData")
	proto.RegisterType((*Request)(nil), "github.com.srcd.berserker.extractor.Request")
	proto.RegisterType((*Service_GetRepositoriesDataResponse)(nil), "github.com.srcd.berserker.extractor.Service_GetRepositoriesDataResponse")
}

// Reference imports to suppress errors if they are not otherwise used.
var _ context.Context
var _ grpc.ClientConn

// This is a compile-time assertion to ensure that this generated file
// is compatible with the grpc package it is being compiled against.
const _ = grpc.SupportPackageIsVersion4

// Client API for ExtractorService service

type ExtractorServiceClient interface {
	Service_GetRepositoriesData(ctx context.Context, in *Request, opts ...grpc.CallOption) (*Service_GetRepositoriesDataResponse, error)
	Service_GetRepositoryData(ctx context.Context, in *Request, opts ...grpc.CallOption) (*RepositoryData, error)
}

type extractorServiceClient struct {
	cc *grpc.ClientConn
}

func NewExtractorServiceClient(cc *grpc.ClientConn) ExtractorServiceClient {
	return &extractorServiceClient{cc}
}

func (c *extractorServiceClient) Service_GetRepositoriesData(ctx context.Context, in *Request, opts ...grpc.CallOption) (*Service_GetRepositoriesDataResponse, error) {
	out := new(Service_GetRepositoriesDataResponse)
	err := grpc.Invoke(ctx, "/github.com.srcd.berserker.extractor.ExtractorService/Service_GetRepositoriesData", in, out, c.cc, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

func (c *extractorServiceClient) Service_GetRepositoryData(ctx context.Context, in *Request, opts ...grpc.CallOption) (*RepositoryData, error) {
	out := new(RepositoryData)
	err := grpc.Invoke(ctx, "/github.com.srcd.berserker.extractor.ExtractorService/Service_GetRepositoryData", in, out, c.cc, opts...)
	if err != nil {
		return nil, err
	}
	return out, nil
}

// Server API for ExtractorService service

type ExtractorServiceServer interface {
	Service_GetRepositoriesData(context.Context, *Request) (*Service_GetRepositoriesDataResponse, error)
	Service_GetRepositoryData(context.Context, *Request) (*RepositoryData, error)
}

func RegisterExtractorServiceServer(s *grpc.Server, srv ExtractorServiceServer) {
	s.RegisterService(&_ExtractorService_serviceDesc, srv)
}

func _ExtractorService_Service_GetRepositoriesData_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(Request)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ExtractorServiceServer).Service_GetRepositoriesData(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/github.com.srcd.berserker.extractor.ExtractorService/Service_GetRepositoriesData",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ExtractorServiceServer).Service_GetRepositoriesData(ctx, req.(*Request))
	}
	return interceptor(ctx, in, info, handler)
}

func _ExtractorService_Service_GetRepositoryData_Handler(srv interface{}, ctx context.Context, dec func(interface{}) error, interceptor grpc.UnaryServerInterceptor) (interface{}, error) {
	in := new(Request)
	if err := dec(in); err != nil {
		return nil, err
	}
	if interceptor == nil {
		return srv.(ExtractorServiceServer).Service_GetRepositoryData(ctx, in)
	}
	info := &grpc.UnaryServerInfo{
		Server:     srv,
		FullMethod: "/github.com.srcd.berserker.extractor.ExtractorService/Service_GetRepositoryData",
	}
	handler := func(ctx context.Context, req interface{}) (interface{}, error) {
		return srv.(ExtractorServiceServer).Service_GetRepositoryData(ctx, req.(*Request))
	}
	return interceptor(ctx, in, info, handler)
}

var _ExtractorService_serviceDesc = grpc.ServiceDesc{
	ServiceName: "github.com.srcd.berserker.extractor.ExtractorService",
	HandlerType: (*ExtractorServiceServer)(nil),
	Methods: []grpc.MethodDesc{
		{
			MethodName: "Service_GetRepositoriesData",
			Handler:    _ExtractorService_Service_GetRepositoriesData_Handler,
		},
		{
			MethodName: "Service_GetRepositoryData",
			Handler:    _ExtractorService_Service_GetRepositoryData_Handler,
		},
	},
	Streams:  []grpc.StreamDesc{},
	Metadata: "github.com/src-d/berserker/extractor/generated.proto",
}

func (m *File) Marshal() (dAtA []byte, err error) {
	size := m.ProtoSize()
	dAtA = make([]byte, size)
	n, err := m.MarshalTo(dAtA)
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *File) MarshalTo(dAtA []byte) (int, error) {
	var i int
	_ = i
	var l int
	_ = l
	if len(m.Language) > 0 {
		dAtA[i] = 0xa
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.Language)))
		i += copy(dAtA[i:], m.Language)
	}
	if len(m.Path) > 0 {
		dAtA[i] = 0x12
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.Path)))
		i += copy(dAtA[i:], m.Path)
	}
	if len(m.UAST) > 0 {
		dAtA[i] = 0x1a
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.UAST)))
		i += copy(dAtA[i:], m.UAST)
	}
	if len(m.Hash) > 0 {
		dAtA[i] = 0x22
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.Hash)))
		i += copy(dAtA[i:], m.Hash)
	}
	return i, nil
}

func (m *RepositoryData) Marshal() (dAtA []byte, err error) {
	size := m.ProtoSize()
	dAtA = make([]byte, size)
	n, err := m.MarshalTo(dAtA)
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *RepositoryData) MarshalTo(dAtA []byte) (int, error) {
	var i int
	_ = i
	var l int
	_ = l
	if len(m.RepositoryID) > 0 {
		dAtA[i] = 0xa
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.RepositoryID)))
		i += copy(dAtA[i:], m.RepositoryID)
	}
	if len(m.URL) > 0 {
		dAtA[i] = 0x12
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.URL)))
		i += copy(dAtA[i:], m.URL)
	}
	if len(m.Files) > 0 {
		for _, msg := range m.Files {
			dAtA[i] = 0x1a
			i++
			i = encodeVarintGenerated(dAtA, i, uint64(msg.ProtoSize()))
			n, err := msg.MarshalTo(dAtA[i:])
			if err != nil {
				return 0, err
			}
			i += n
		}
	}
	return i, nil
}

func (m *Request) Marshal() (dAtA []byte, err error) {
	size := m.ProtoSize()
	dAtA = make([]byte, size)
	n, err := m.MarshalTo(dAtA)
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *Request) MarshalTo(dAtA []byte) (int, error) {
	var i int
	_ = i
	var l int
	_ = l
	if len(m.RepositoryIDs) > 0 {
		for _, s := range m.RepositoryIDs {
			dAtA[i] = 0xa
			i++
			l = len(s)
			for l >= 1<<7 {
				dAtA[i] = uint8(uint64(l)&0x7f | 0x80)
				l >>= 7
				i++
			}
			dAtA[i] = uint8(l)
			i++
			i += copy(dAtA[i:], s)
		}
	}
	if len(m.RootCommitHash) > 0 {
		dAtA[i] = 0x12
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.RootCommitHash)))
		i += copy(dAtA[i:], m.RootCommitHash)
	}
	if len(m.Reference) > 0 {
		dAtA[i] = 0x1a
		i++
		i = encodeVarintGenerated(dAtA, i, uint64(len(m.Reference)))
		i += copy(dAtA[i:], m.Reference)
	}
	return i, nil
}

func (m *Service_GetRepositoriesDataResponse) Marshal() (dAtA []byte, err error) {
	size := m.ProtoSize()
	dAtA = make([]byte, size)
	n, err := m.MarshalTo(dAtA)
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *Service_GetRepositoriesDataResponse) MarshalTo(dAtA []byte) (int, error) {
	var i int
	_ = i
	var l int
	_ = l
	if len(m.Result1) > 0 {
		for _, msg := range m.Result1 {
			dAtA[i] = 0xa
			i++
			i = encodeVarintGenerated(dAtA, i, uint64(msg.ProtoSize()))
			n, err := msg.MarshalTo(dAtA[i:])
			if err != nil {
				return 0, err
			}
			i += n
		}
	}
	return i, nil
}

func encodeFixed64Generated(dAtA []byte, offset int, v uint64) int {
	dAtA[offset] = uint8(v)
	dAtA[offset+1] = uint8(v >> 8)
	dAtA[offset+2] = uint8(v >> 16)
	dAtA[offset+3] = uint8(v >> 24)
	dAtA[offset+4] = uint8(v >> 32)
	dAtA[offset+5] = uint8(v >> 40)
	dAtA[offset+6] = uint8(v >> 48)
	dAtA[offset+7] = uint8(v >> 56)
	return offset + 8
}
func encodeFixed32Generated(dAtA []byte, offset int, v uint32) int {
	dAtA[offset] = uint8(v)
	dAtA[offset+1] = uint8(v >> 8)
	dAtA[offset+2] = uint8(v >> 16)
	dAtA[offset+3] = uint8(v >> 24)
	return offset + 4
}
func encodeVarintGenerated(dAtA []byte, offset int, v uint64) int {
	for v >= 1<<7 {
		dAtA[offset] = uint8(v&0x7f | 0x80)
		v >>= 7
		offset++
	}
	dAtA[offset] = uint8(v)
	return offset + 1
}
func (m *File) ProtoSize() (n int) {
	var l int
	_ = l
	l = len(m.Language)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	l = len(m.Path)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	l = len(m.UAST)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	l = len(m.Hash)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	return n
}

func (m *RepositoryData) ProtoSize() (n int) {
	var l int
	_ = l
	l = len(m.RepositoryID)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	l = len(m.URL)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	if len(m.Files) > 0 {
		for _, e := range m.Files {
			l = e.ProtoSize()
			n += 1 + l + sovGenerated(uint64(l))
		}
	}
	return n
}

func (m *Request) ProtoSize() (n int) {
	var l int
	_ = l
	if len(m.RepositoryIDs) > 0 {
		for _, s := range m.RepositoryIDs {
			l = len(s)
			n += 1 + l + sovGenerated(uint64(l))
		}
	}
	l = len(m.RootCommitHash)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	l = len(m.Reference)
	if l > 0 {
		n += 1 + l + sovGenerated(uint64(l))
	}
	return n
}

func (m *Service_GetRepositoriesDataResponse) ProtoSize() (n int) {
	var l int
	_ = l
	if len(m.Result1) > 0 {
		for _, e := range m.Result1 {
			l = e.ProtoSize()
			n += 1 + l + sovGenerated(uint64(l))
		}
	}
	return n
}

func sovGenerated(x uint64) (n int) {
	for {
		n++
		x >>= 7
		if x == 0 {
			break
		}
	}
	return n
}
func sozGenerated(x uint64) (n int) {
	return sovGenerated(uint64((x << 1) ^ uint64((int64(x) >> 63))))
}
func (m *File) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowGenerated
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: File: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: File: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Language", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Language = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 2:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Path", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Path = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 3:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field UAST", wireType)
			}
			var byteLen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				byteLen |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if byteLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + byteLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.UAST = append(m.UAST[:0], dAtA[iNdEx:postIndex]...)
			if m.UAST == nil {
				m.UAST = []byte{}
			}
			iNdEx = postIndex
		case 4:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Hash", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Hash = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		default:
			iNdEx = preIndex
			skippy, err := skipGenerated(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if skippy < 0 {
				return ErrInvalidLengthGenerated
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func (m *RepositoryData) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowGenerated
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: RepositoryData: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: RepositoryData: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field RepositoryID", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.RepositoryID = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 2:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field URL", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.URL = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 3:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Files", wireType)
			}
			var msglen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				msglen |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if msglen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + msglen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Files = append(m.Files, File{})
			if err := m.Files[len(m.Files)-1].Unmarshal(dAtA[iNdEx:postIndex]); err != nil {
				return err
			}
			iNdEx = postIndex
		default:
			iNdEx = preIndex
			skippy, err := skipGenerated(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if skippy < 0 {
				return ErrInvalidLengthGenerated
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func (m *Request) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowGenerated
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: Request: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: Request: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field RepositoryIDs", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.RepositoryIDs = append(m.RepositoryIDs, string(dAtA[iNdEx:postIndex]))
			iNdEx = postIndex
		case 2:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field RootCommitHash", wireType)
			}
			var byteLen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				byteLen |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if byteLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + byteLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.RootCommitHash = append(m.RootCommitHash[:0], dAtA[iNdEx:postIndex]...)
			if m.RootCommitHash == nil {
				m.RootCommitHash = []byte{}
			}
			iNdEx = postIndex
		case 3:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Reference", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= (uint64(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + intStringLen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Reference = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		default:
			iNdEx = preIndex
			skippy, err := skipGenerated(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if skippy < 0 {
				return ErrInvalidLengthGenerated
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func (m *Service_GetRepositoriesDataResponse) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowGenerated
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: Service_GetRepositoriesDataResponse: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: Service_GetRepositoriesDataResponse: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Result1", wireType)
			}
			var msglen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				msglen |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if msglen < 0 {
				return ErrInvalidLengthGenerated
			}
			postIndex := iNdEx + msglen
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Result1 = append(m.Result1, &RepositoryData{})
			if err := m.Result1[len(m.Result1)-1].Unmarshal(dAtA[iNdEx:postIndex]); err != nil {
				return err
			}
			iNdEx = postIndex
		default:
			iNdEx = preIndex
			skippy, err := skipGenerated(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if skippy < 0 {
				return ErrInvalidLengthGenerated
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func skipGenerated(dAtA []byte) (n int, err error) {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return 0, ErrIntOverflowGenerated
			}
			if iNdEx >= l {
				return 0, io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		wireType := int(wire & 0x7)
		switch wireType {
		case 0:
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return 0, ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return 0, io.ErrUnexpectedEOF
				}
				iNdEx++
				if dAtA[iNdEx-1] < 0x80 {
					break
				}
			}
			return iNdEx, nil
		case 1:
			iNdEx += 8
			return iNdEx, nil
		case 2:
			var length int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return 0, ErrIntOverflowGenerated
				}
				if iNdEx >= l {
					return 0, io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				length |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			iNdEx += length
			if length < 0 {
				return 0, ErrInvalidLengthGenerated
			}
			return iNdEx, nil
		case 3:
			for {
				var innerWire uint64
				var start int = iNdEx
				for shift := uint(0); ; shift += 7 {
					if shift >= 64 {
						return 0, ErrIntOverflowGenerated
					}
					if iNdEx >= l {
						return 0, io.ErrUnexpectedEOF
					}
					b := dAtA[iNdEx]
					iNdEx++
					innerWire |= (uint64(b) & 0x7F) << shift
					if b < 0x80 {
						break
					}
				}
				innerWireType := int(innerWire & 0x7)
				if innerWireType == 4 {
					break
				}
				next, err := skipGenerated(dAtA[start:])
				if err != nil {
					return 0, err
				}
				iNdEx = start + next
			}
			return iNdEx, nil
		case 4:
			return iNdEx, nil
		case 5:
			iNdEx += 4
			return iNdEx, nil
		default:
			return 0, fmt.Errorf("proto: illegal wireType %d", wireType)
		}
	}
	panic("unreachable")
}

var (
	ErrInvalidLengthGenerated = fmt.Errorf("proto: negative length found during unmarshaling")
	ErrIntOverflowGenerated   = fmt.Errorf("proto: integer overflow")
)

func init() {
	proto.RegisterFile("github.com/src-d/berserker/extractor/generated.proto", fileDescriptorGenerated)
}

var fileDescriptorGenerated = []byte{
	// 507 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0x9c, 0x93, 0x41, 0x6e, 0xd3, 0x40,
	0x14, 0x86, 0x33, 0xb5, 0x21, 0xc9, 0x34, 0x89, 0xc2, 0xac, 0xdc, 0x80, 0xe2, 0xa8, 0xdd, 0x04,
	0x89, 0x3a, 0x22, 0x05, 0x09, 0xb1, 0xc3, 0xb4, 0xd0, 0x4a, 0xb0, 0x99, 0xd2, 0x0d, 0x9b, 0x68,
	0x62, 0xbf, 0x38, 0x16, 0x4e, 0x26, 0xcc, 0x8c, 0x11, 0xdd, 0xb0, 0xe6, 0x02, 0x95, 0x58, 0x96,
	0x2d, 0x5b, 0x2e, 0xd1, 0x25, 0x27, 0xb0, 0x90, 0x7b, 0x01, 0x8e, 0x80, 0x3c, 0xa6, 0x8e, 0x2b,
	0xa1, 0xca, 0xea, 0x6e, 0xde, 0xaf, 0xff, 0x7f, 0xef, 0xcb, 0x1f, 0x19, 0x3f, 0x09, 0x42, 0x35,
	0x8f, 0xa7, 0x8e, 0xc7, 0x17, 0x23, 0x29, 0xbc, 0x5d, 0x7f, 0x34, 0x05, 0x21, 0x41, 0x7c, 0x00,
	0x31, 0x82, 0xcf, 0x4a, 0x30, 0x4f, 0x71, 0x31, 0x0a, 0x60, 0x09, 0x82, 0x29, 0xf0, 0x9d, 0x95,
	0xe0, 0x8a, 0x93, 0x9d, 0x75, 0xca, 0x91, 0xc2, 0xf3, 0x9d, 0x22, 0xe4, 0x14, 0xa1, 0xde, 0x6e,
	0x69, 0x75, 0xc0, 0x03, 0x3e, 0xd2, 0xd9, 0x69, 0x3c, 0xd3, 0x93, 0x1e, 0xf4, 0x2b, 0xdf, 0xb9,
	0x2d, 0xb0, 0xf9, 0x2a, 0x8c, 0x80, 0xf4, 0x70, 0x23, 0x62, 0xcb, 0x20, 0x66, 0x01, 0x58, 0x68,
	0x80, 0x86, 0x4d, 0x5a, 0xcc, 0x84, 0x60, 0x73, 0xc5, 0xd4, 0xdc, 0xda, 0xd0, 0xba, 0x7e, 0x93,
	0x07, 0xd8, 0x8c, 0x99, 0x54, 0x96, 0x31, 0x40, 0xc3, 0x96, 0xdb, 0x48, 0x13, 0xdb, 0x3c, 0x79,
	0x71, 0xfc, 0x8e, 0x6a, 0x35, 0x4b, 0xcc, 0x99, 0x9c, 0x5b, 0x66, 0x9e, 0xc8, 0xde, 0xcf, 0x1b,
	0x5f, 0xcf, 0xed, 0xda, 0x9f, 0xef, 0x76, 0x6d, 0xfb, 0x27, 0xc2, 0x1d, 0x0a, 0x2b, 0x2e, 0x43,
	0xc5, 0xc5, 0xe9, 0x3e, 0x53, 0x8c, 0x3c, 0xc5, 0x6d, 0x51, 0x28, 0x93, 0xd0, 0xcf, 0x19, 0xdc,
	0x6e, 0x9a, 0xd8, 0xad, 0xb5, 0xf5, 0x68, 0x9f, 0xb6, 0xd6, 0xb6, 0x23, 0x9f, 0x6c, 0x61, 0x23,
	0x16, 0x51, 0x0e, 0xe6, 0xd6, 0xd3, 0xc4, 0x36, 0x4e, 0xe8, 0x1b, 0x9a, 0x69, 0xe4, 0x00, 0xdf,
	0x99, 0x85, 0x11, 0x48, 0xcb, 0x18, 0x18, 0xc3, 0xcd, 0xf1, 0x43, 0xa7, 0x42, 0x79, 0x4e, 0x56,
	0x85, 0x6b, 0x5e, 0x24, 0x76, 0x8d, 0xe6, 0xe9, 0x12, 0xf5, 0x19, 0xc2, 0x75, 0x0a, 0x1f, 0x63,
	0x90, 0x8a, 0x3c, 0xc3, 0x9d, 0x6b, 0xb8, 0xd2, 0x42, 0x03, 0x63, 0xd8, 0x74, 0xef, 0xa5, 0x89,
	0xdd, 0x2e, 0xf3, 0x4a, 0xda, 0x2e, 0x03, 0x4b, 0x32, 0xc4, 0x5d, 0xc1, 0xb9, 0x9a, 0x78, 0x7c,
	0xb1, 0x08, 0xd5, 0x44, 0xb7, 0x94, 0xe1, 0xb7, 0x68, 0x27, 0xd3, 0x5f, 0x6a, 0xf9, 0x90, 0xc9,
	0xac, 0xe1, 0xa6, 0x80, 0x19, 0x08, 0x58, 0x7a, 0xa0, 0x6b, 0x6e, 0xd2, 0xb5, 0x50, 0xe2, 0x52,
	0x78, 0xe7, 0x18, 0xc4, 0xa7, 0xd0, 0x83, 0xc9, 0x6b, 0x50, 0xc5, 0xf1, 0x10, 0x64, 0xd6, 0x2c,
	0x05, 0xb9, 0xe2, 0x4b, 0x09, 0xe4, 0x2d, 0xae, 0x0b, 0x90, 0x71, 0xa4, 0x1e, 0x6b, 0xd6, 0xcd,
	0xf1, 0x5e, 0xa5, 0x46, 0xae, 0xff, 0x4f, 0xf4, 0x6a, 0xc7, 0xf8, 0xc7, 0x06, 0xee, 0x1e, 0x5c,
	0xb9, 0xfe, 0xdd, 0x27, 0x67, 0x08, 0xdf, 0xbf, 0x81, 0x85, 0x3c, 0xaa, 0x78, 0x52, 0x97, 0xdc,
	0x3b, 0xac, 0xe4, 0xae, 0xf2, 0xdb, 0xbf, 0xe0, 0xad, 0xff, 0xd9, 0x4e, 0x6f, 0x01, 0x75, 0x9b,
	0xd6, 0x5c, 0xfb, 0x22, 0xed, 0xa3, 0x5f, 0x69, 0x1f, 0xfd, 0x4e, 0xfb, 0xb5, 0x6f, 0x97, 0xfd,
	0xda, 0xf9, 0x65, 0x1f, 0xbd, 0x6f, 0x16, 0xfe, 0xe9, 0x5d, 0xfd, 0x31, 0xee, 0xfd, 0x0d, 0x00,
	0x00, 0xff, 0xff, 0xa9, 0x4d, 0x82, 0x1e, 0x18, 0x04, 0x00, 0x00,
}
